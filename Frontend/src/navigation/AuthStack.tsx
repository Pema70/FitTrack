import { createNativeStackNavigator } from '@react-navigation/native-stack';
import LoginScreen from '../screens/auth/LoginScreen'; // Importujemy nowy ekran

const Stack = createNativeStackNavigator();

export default function AuthStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      {/* Dodajemy ekran logowania jako pierwsze i jedyne dziecko w tym stosie */}
      <Stack.Screen name="Login" component={LoginScreen} />
    </Stack.Navigator>
  );
}