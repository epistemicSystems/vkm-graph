/**
 * UploadZone Component
 *
 * Beautiful drag-and-drop document upload interface.
 * Accepts text files and displays upload progress.
 */

import React, { useState, useCallback } from 'react';
import { uploadDocument } from '@/api/client';

export interface UploadZoneProps {
  onUploadSuccess?: (patchId: string, factsCount: number) => void;
  onUploadError?: (error: string) => void;
}

export const UploadZone: React.FC<UploadZoneProps> = ({
  onUploadSuccess,
  onUploadError,
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState<{
    type: 'success' | 'error' | null;
    message: string;
  }>({ type: null, message: '' });

  const handleDragEnter = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  }, []);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  }, []);

  const processFile = async (file: File) => {
    setIsUploading(true);
    setUploadStatus({ type: null, message: '' });

    try {
      const content = await file.text();

      if (!content || content.trim().length === 0) {
        throw new Error('File is empty');
      }

      const response = await uploadDocument(content, file.name);

      if (response.error) {
        throw new Error(response.error);
      }

      if (!response.data) {
        throw new Error('No data returned from server');
      }

      const { 'patch-id': patchId, 'facts-count': factsCount, message } = response.data;

      setUploadStatus({
        type: 'success',
        message: `✓ ${message} (${factsCount} facts extracted)`,
      });

      onUploadSuccess?.(patchId, factsCount);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Upload failed';
      setUploadStatus({
        type: 'error',
        message: `✗ ${errorMessage}`,
      });
      onUploadError?.(errorMessage);
    } finally {
      setIsUploading(false);
      setIsDragging(false);
    }
  };

  const handleDrop = useCallback(
    async (e: React.DragEvent) => {
      e.preventDefault();
      e.stopPropagation();
      setIsDragging(false);

      const { files } = e.dataTransfer;

      if (files.length === 0) {
        return;
      }

      const file = files[0];

      // Validate file type
      if (!file.type.startsWith('text/') && !file.name.endsWith('.txt')) {
        setUploadStatus({
          type: 'error',
          message: '✗ Please upload a text file (.txt)',
        });
        return;
      }

      await processFile(file);
    },
    [onUploadSuccess, onUploadError]
  );

  const handleFileInput = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const { files } = e.target;

      if (!files || files.length === 0) {
        return;
      }

      await processFile(files[0]);

      // Reset input
      e.target.value = '';
    },
    [onUploadSuccess, onUploadError]
  );

  return (
    <div className="upload-zone-container">
      <div
        className={`upload-zone ${isDragging ? 'dragging' : ''} ${
          isUploading ? 'uploading' : ''
        }`}
        onDragEnter={handleDragEnter}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
      >
        <input
          type="file"
          id="file-upload"
          className="file-input"
          accept=".txt,text/plain"
          onChange={handleFileInput}
          disabled={isUploading}
        />

        <label htmlFor="file-upload" className="upload-label">
          {isUploading ? (
            <>
              <div className="spinner" />
              <p className="upload-text">Processing document...</p>
            </>
          ) : (
            <>
              <svg
                className="upload-icon"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
                />
              </svg>
              <p className="upload-text">
                {isDragging ? (
                  <strong>Drop document here</strong>
                ) : (
                  <>
                    <strong>Drop a document</strong> or click to upload
                  </>
                )}
              </p>
              <p className="upload-hint">Text files (.txt) accepted</p>
            </>
          )}
        </label>
      </div>

      {uploadStatus.type && (
        <div className={`upload-status ${uploadStatus.type}`}>
          {uploadStatus.message}
        </div>
      )}
    </div>
  );
};

export default UploadZone;
