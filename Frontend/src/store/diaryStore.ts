// src/store/diaryStore.ts
import { create } from 'zustand';

interface DiaryEntry { id: string; productName: string; kcal: number; date: string; }

interface DiaryState {
  entries: DiaryEntry[];
  addEntry: (e: DiaryEntry) => void;
  removeEntry: (id: string) => void;
}

export const useDiaryStore = create<DiaryState>()(set => ({
  entries: [],
  addEntry: e => set(s => ({ entries: [...s.entries, e] })),
  removeEntry: id => set(s => ({ entries: s.entries.filter(x => x.id !== id) })),
}));