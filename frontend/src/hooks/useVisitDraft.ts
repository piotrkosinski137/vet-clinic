import { useState, useCallback, useRef, useEffect } from 'react';
import { api } from '../api/client';
import type { VisitDraftDto } from '../api/types';

const DEBOUNCE_MS = 2000; // Save draft 2 seconds after last change

interface UseVisitDraftResult {
  /** Debounced save function - call on every form change */
  saveDraft: (draft: VisitDraftDto) => void;
  /** Load existing draft for a visit */
  loadDraft: () => Promise<VisitDraftDto | null>;
  /** Force immediate save (call before closing modal if needed) */
  flushDraft: () => Promise<void>;
  /** Clear pending draft without saving */
  clearPending: () => void;
  /** Whether a draft save is in progress */
  isSaving: boolean;
  /** When the draft was last saved successfully */
  lastSaved: Date | null;
  /** Error message if last save failed */
  error: string | null;
  /** Whether there are pending unsaved changes */
  hasPendingChanges: boolean;
}

/**
 * Hook for managing visit draft auto-save functionality.
 *
 * Provides debounced auto-save, draft loading, and save status tracking.
 * Drafts are stored in the database for cross-device reliability.
 *
 * @example
 * ```tsx
 * function VisitDetailsModal({ visit }) {
 *   const { saveDraft, loadDraft, isSaving, lastSaved } = useVisitDraft(visit?.id);
 *
 *   useEffect(() => {
 *     if (visit?.id) {
 *       loadDraft().then(draft => {
 *         if (draft) setFormData(draft);
 *       });
 *     }
 *   }, [visit?.id]);
 *
 *   const handleFieldChange = (field, value) => {
 *     setFormData(prev => {
 *       const newData = { ...prev, [field]: value };
 *       saveDraft(newData); // Auto-saved after 2 seconds
 *       return newData;
 *     });
 *   };
 * }
 * ```
 */
export function useVisitDraft(visitId: string | undefined): UseVisitDraftResult {
  const [isSaving, setIsSaving] = useState(false);
  const [lastSaved, setLastSaved] = useState<Date | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [hasPendingChanges, setHasPendingChanges] = useState(false);

  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const pendingDraftRef = useRef<VisitDraftDto | null>(null);
  const visitIdRef = useRef<string | undefined>(visitId);

  // Keep visitId ref updated
  useEffect(() => {
    visitIdRef.current = visitId;
  }, [visitId]);

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  // Reset state when visitId changes
  useEffect(() => {
    setLastSaved(null);
    setError(null);
    setHasPendingChanges(false);
    pendingDraftRef.current = null;
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
  }, [visitId]);

  // Debounced save function
  const saveDraft = useCallback((draft: VisitDraftDto) => {
    if (!visitIdRef.current) return;

    // Store pending draft
    pendingDraftRef.current = draft;
    setHasPendingChanges(true);
    setError(null);

    // Clear existing timeout
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    // Set new timeout
    timeoutRef.current = setTimeout(async () => {
      const currentVisitId = visitIdRef.current;
      const currentDraft = pendingDraftRef.current;

      if (!currentVisitId || !currentDraft) return;

      setIsSaving(true);
      try {
        await api.saveVisitDraft(currentVisitId, currentDraft);
        setLastSaved(new Date());
        setHasPendingChanges(false);
        pendingDraftRef.current = null;
      } catch (err) {
        console.error('Failed to save draft:', err);
        setError('Failed to save draft');
        // Don't clear pending draft - will retry on next change
      } finally {
        setIsSaving(false);
      }
    }, DEBOUNCE_MS);
  }, []);

  // Load existing draft
  const loadDraft = useCallback(async (): Promise<VisitDraftDto | null> => {
    if (!visitIdRef.current) return null;

    try {
      const draft = await api.getVisitDraft(visitIdRef.current);
      if (draft?.savedAt) {
        setLastSaved(new Date(draft.savedAt));
      }
      return draft;
    } catch (err) {
      console.error('Failed to load draft:', err);
      return null;
    }
  }, []);

  // Force immediate save (call before closing modal if needed)
  const flushDraft = useCallback(async () => {
    const currentVisitId = visitIdRef.current;
    const currentDraft = pendingDraftRef.current;

    if (!currentVisitId || !currentDraft) return;

    // Clear pending timeout
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }

    setIsSaving(true);
    try {
      await api.saveVisitDraft(currentVisitId, currentDraft);
      setLastSaved(new Date());
      setHasPendingChanges(false);
      pendingDraftRef.current = null;
    } catch (err) {
      console.error('Failed to flush draft:', err);
      // Don't throw - we're closing anyway
    } finally {
      setIsSaving(false);
    }
  }, []);

  // Clear pending draft without saving
  const clearPending = useCallback(() => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
    pendingDraftRef.current = null;
    setHasPendingChanges(false);
  }, []);

  return {
    saveDraft,
    loadDraft,
    flushDraft,
    clearPending,
    isSaving,
    lastSaved,
    error,
    hasPendingChanges,
  };
}
