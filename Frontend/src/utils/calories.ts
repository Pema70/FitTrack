// src/utils/calories.ts
export function calculateBMR(weight: number, height: number, age: number, sex: 'M' | 'F') {
  if (sex === 'M') return 88.36 + 13.4 * weight + 4.8 * height - 5.7 * age;
  return 447.6 + 9.2 * weight + 3.1 * height - 4.3 * age;
}

export function calculateTDEE(bmr: number, activityLevel: number) {
  // 1.2 siedzący | 1.375 lekki | 1.55 umiarkowany | 1.725 aktywny
  return Math.round(bmr * activityLevel);
}