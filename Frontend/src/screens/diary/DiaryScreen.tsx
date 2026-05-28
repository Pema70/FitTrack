// // src/screens/diary/DiaryScreen.tsx
// import { useQuery } from '@tanstack/react-query';
// import { getDailyDiary } from '../api/diary';

// export default function DiaryScreen() {
//   const { data, isLoading } = useQuery({
//     queryKey: ['diary', 'today'],
//     queryFn: getDailyDiary,
//   });

//   const totalKcal = data?.entries.reduce((acc, e) => acc + e.kcal, 0) ?? 0;
//   const goal = useAuthStore(s => s.user?.calorieGoal) ?? 2000;
//   const remaining = goal - totalKcal;

//   if (isLoading) return <ActivityIndicator />;

//   return (
//     <View style={styles.container}>
//       {/* Kołowy wskaźnik kalorii */}
//       <CalorieRing eaten={totalKcal} goal={goal} remaining={remaining} />
//       {/* Lista posiłków z obsługą swipe-to-delete */}
//       <DiaryList entries={data?.entries ?? []} />
//       {/* FAB do dodawania */}
//       <FAB onPress={() => navigation.navigate('Search')} />
//     </View>
//   );
// }