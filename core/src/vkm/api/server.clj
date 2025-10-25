(ns vkm.api.server
  "HTTP API server for the VKM Graph system.

   Exposes endpoints for:
   - Document upload and processing
   - Patch querying
   - Database statistics

   Run with: clj -M:server"
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.util.response :as response]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [cheshire.core :as json]
            [vkm.db :as db]
            [vkm.patch :as patch]
            [vkm.semantic :as semantic]
            [vkm.pipeline :as pipeline])
  (:gen-class))

;; ============================================================
;; Configuration
;; ============================================================

(def server-config
  {:port 3000
   :join? false})

;; ============================================================
;; Helper Functions
;; ============================================================

(defn edn->json
  "Convert EDN data to JSON-friendly format.

   Converts Clojure keywords to strings and handles dates."
  [data]
  (json/generate-string data {:key-fn name
                               :date-format "yyyy-MM-dd'T'HH:mm:ss'Z'"}))

(defn success-response
  "Create a success JSON response."
  [data]
  (-> (response/response data)
      (response/status 200)
      (response/content-type "application/json")))

(defn error-response
  "Create an error JSON response."
  [message & {:keys [status] :or {status 400}}]
  (-> (response/response {:error message})
      (response/status status)
      (response/content-type "application/json")))

;; ============================================================
;; API Handlers
;; ============================================================

(defn health-handler
  "Health check endpoint."
  [_]
  (success-response {:status "ok"
                     :service "vkm-graph-api"
                     :version "0.1.0"}))

(defn stats-handler
  "Get database statistics."
  [_]
  (try
    (let [stats (db/db-stats)]
      (success-response stats))
    (catch Exception e
      (log/error "Failed to get stats:" (.getMessage e))
      (error-response "Failed to retrieve statistics" :status 500))))

(defn list-patches-handler
  "List all patches."
  [request]
  (try
    (let [source-id (get-in request [:params :source-id])
          patches (if source-id
                   (map db/pull-patch (db/find-patches-by-source source-id))
                   [])]
      (success-response {:patches patches
                        :count (count patches)}))
    (catch Exception e
      (log/error "Failed to list patches:" (.getMessage e))
      (error-response "Failed to list patches" :status 500))))

(defn get-patch-handler
  "Get a specific patch by ID."
  [request]
  (try
    (let [patch-id (get-in request [:params :id])]
      (if-let [patch (db/pull-patch patch-id)]
        (success-response patch)
        (error-response "Patch not found" :status 404)))
    (catch Exception e
      (log/error "Failed to get patch:" (.getMessage e))
      (error-response "Failed to retrieve patch" :status 500))))

(defn upload-document-handler
  "Process uploaded document and create patches.

   Expects multipart form with 'file' field containing document text."
  [request]
  (try
    (let [body (get request :body)
          content (get body "content")
          filename (get body "filename" "document.txt")]

      (if (empty? content)
        (error-response "No content provided")

        (do
          (log/info "Processing document:" filename)

          ;; Extract facts from content
          (let [facts (semantic/extract-facts-from-text content)

                ;; Build patch
                patch (patch/make-patch
                       {:source :document
                        :source-id filename
                        :facts facts
                        :edges []})

                ;; Store patch
                patch-id (db/store-patch! patch)]

            (log/info "Created patch:" patch-id "with" (count facts) "facts")

            (success-response {:patch-id patch-id
                              :facts-count (count facts)
                              :message "Document processed successfully"})))))

    (catch Exception e
      (log/error "Failed to process document:" (.getMessage e))
      (error-response (.getMessage e) :status 500))))

(defn process-transcripts-handler
  "Process transcripts directory and create patches.

   Body: { source-id, transcripts-dir }"
  [request]
  (try
    (let [body (get request :body)
          source-id (get body "source-id")
          transcripts-dir (get body "transcripts-dir")]

      (if (or (empty? source-id) (empty? transcripts-dir))
        (error-response "Missing source-id or transcripts-dir")

        (do
          (log/info "Processing transcripts from:" transcripts-dir)

          ;; Run pipeline
          (let [result (pipeline/process-source source-id transcripts-dir)
                patch-id (:patch-id result)
                motives (:motives result)]

            (success-response {:patch-id patch-id
                              :motives-count (count motives)
                              :message "Transcripts processed successfully"})))))

    (catch Exception e
      (log/error "Failed to process transcripts:" (.getMessage e))
      (error-response (.getMessage e) :status 500))))

(defn query-patches-handler
  "Query patches with filters.

   Query params:
   - topic: Filter by topic keyword
   - from-date: ISO timestamp
   - to-date: ISO timestamp
   - min-confidence: Float 0-1"
  [request]
  (try
    (let [params (:params request)
          topic (when-let [t (:topic params)] (keyword t))
          ;; For now, simple query by topic
          results (if topic
                   (db/query-facts-by-topic topic)
                   [])]

      (success-response {:facts results
                        :count (count results)}))

    (catch Exception e
      (log/error "Failed to query patches:" (.getMessage e))
      (error-response "Query failed" :status 500))))

;; ============================================================
;; Routes
;; ============================================================

(defroutes app-routes
  ;; Health check
  (GET "/health" [] health-handler)

  ;; Database stats
  (GET "/api/stats" [] stats-handler)

  ;; Patches
  (GET "/api/patches" [] list-patches-handler)
  (GET "/api/patches/:id" [] get-patch-handler)
  (POST "/api/patches/query" [] query-patches-handler)

  ;; Upload & processing
  (POST "/api/upload" [] upload-document-handler)
  (POST "/api/process" [] process-transcripts-handler)

  ;; 404
  (route/not-found (error-response "Endpoint not found" :status 404)))

;; ============================================================
;; Middleware
;; ============================================================

(defn wrap-logging
  "Log all requests."
  [handler]
  (fn [request]
    (log/info (:request-method request) (:uri request))
    (handler request)))

(defn wrap-exception-handling
  "Catch and log exceptions."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/error "Unhandled exception:" (.getMessage e))
        (error-response "Internal server error" :status 500)))))

(def app
  "Application with middleware stack."
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response
      wrap-params
      (wrap-cors :access-control-allow-origin [#"http://localhost:5173"
                                                 #"http://localhost:3000"]
                 :access-control-allow-methods [:get :post :put :delete]
                 :access-control-allow-headers ["Content-Type" "Authorization"])
      wrap-logging
      wrap-exception-handling))

;; ============================================================
;; Server Control
;; ============================================================

(defonce server (atom nil))

(defn start-server!
  "Start the HTTP server."
  ([]
   (start-server! server-config))
  ([config]
   (when-not @server
     (log/info "Starting server on port" (:port config))

     ;; Initialize database
     (db/init-db! "datomic:mem://knowledge-graph"
                  :schema-path "resources/schema/datomic-schema.edn")

     ;; Start Jetty
     (reset! server (jetty/run-jetty app (assoc config :join? false)))

     (log/info "Server started on http://localhost:" (:port config)))))

(defn stop-server!
  "Stop the HTTP server."
  []
  (when @server
    (log/info "Stopping server")
    (.stop @server)
    (reset! server nil)))

(defn restart-server!
  "Restart the HTTP server."
  []
  (stop-server!)
  (Thread/sleep 100)
  (start-server!))

;; ============================================================
;; Main Entry Point
;; ============================================================

(defn -main
  "Start the API server."
  [& args]
  (let [port (if (first args)
              (Integer/parseInt (first args))
              3000)]
    (start-server! {:port port :join? true})))

;; ============================================================
;; REPL Helpers
;; ============================================================

(comment
  ;; Start server
  (start-server!)

  ;; Stop server
  (stop-server!)

  ;; Restart server
  (restart-server!)

  ;; Test endpoints
  (require '[clj-http.client :as http])

  ;; Health check
  (http/get "http://localhost:3000/health")

  ;; Stats
  (http/get "http://localhost:3000/api/stats")

  ;; Upload document
  (http/post "http://localhost:3000/api/upload"
             {:body (json/generate-string
                     {:content "Large models learn better than small models."
                      :filename "test.txt"})
              :content-type :json
              :as :json}))
