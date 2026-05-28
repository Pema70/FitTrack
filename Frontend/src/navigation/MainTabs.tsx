// src/navigation/MainTabs.tsx
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
// import DiaryScreen from '../screens/diary/DiaryScreen';
import RecipesScreen from '../screens/recipes/RecipesScreen';
import WorkoutScreen from '../screens/workout/WorkoutScreen';
import ProfileScreen from '../screens/profile/ProfileScreen';

const Tab = createBottomTabNavigator();

export default function MainTabs() {
  return (
    <Tab.Navigator screenOptions={{ headerShown: false }}>
      {/* <Tab.Screen name="Dziennik" component={DiaryScreen} /> */}
      <Tab.Screen name="Przepisy" component={RecipesScreen} />
      <Tab.Screen name="Trening"  component={WorkoutScreen} />
      <Tab.Screen name="Profil"   component={ProfileScreen} />
    </Tab.Navigator>
  );
}