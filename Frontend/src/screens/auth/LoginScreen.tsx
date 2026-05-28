import React, { useState } from 'react';
import { 
  View, 
  Text, 
  TextInput, 
  TouchableOpacity, 
  StyleSheet, 
  KeyboardAvoidingView, 
  Platform 
} from 'react-native';

export default function LoginScreen() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = () => {
    // W przyszłości tutaj dodasz logikę sprawdzania poprawności danych 
    // lub wywołanie API do logowania.
    console.log('Próba logowania:', email);
  };

  return (
    // KeyboardAvoidingView zapobiega zasłanianiu pól przez klawiaturę
    <KeyboardAvoidingView 
      style={styles.container} 
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <View style={styles.formContainer}>
        {/* Nagłówek aplikacji */}
        <Text style={styles.title}>FitTrack</Text>
        <Text style={styles.subtitle}>Zaloguj się, aby zacząć trening</Text>

        {/* Pola formularza */}
        <TextInput
          style={styles.input}
          placeholder="Adres e-mail"
          placeholderTextColor="#888"
          value={email}
          onChangeText={setEmail}
          keyboardType="email-address"
          autoCapitalize="none"
        />

        <TextInput
          style={styles.input}
          placeholder="Hasło"
          placeholderTextColor="#888"
          value={password}
          onChangeText={setPassword}
          secureTextEntry // Ukrywa wpisywane znaki
        />

        {/* Główny przycisk logowania */}
        <TouchableOpacity style={styles.loginButton} onPress={handleLogin}>
          <Text style={styles.loginButtonText}>Zaloguj się</Text>
        </TouchableOpacity>

        {/* Opcjonalny link do rejestracji */}
        <TouchableOpacity style={styles.registerLink}>
          <Text style={styles.registerLinkText}>Nie masz konta? Zarejestruj się</Text>
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

// Style (Design dopasowany do nowoczesnej aplikacji fitness)
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#121212', // Ciemny, sportowy motyw
    justifyContent: 'center',
  },
  formContainer: {
    paddingHorizontal: 30,
  },
  title: {
    fontSize: 42,
    fontWeight: 'bold',
    color: '#FF5722', // Energetyczny, pomarańczowy kolor (fitness vibe)
    textAlign: 'center',
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 16,
    color: '#BBBBBB',
    textAlign: 'center',
    marginBottom: 40,
  },
  input: {
    backgroundColor: '#1E1E1E',
    color: '#FFFFFF',
    borderRadius: 10,
    padding: 15,
    marginBottom: 20,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#333',
  },
  loginButton: {
    backgroundColor: '#FF5722',
    padding: 15,
    borderRadius: 10,
    alignItems: 'center',
    marginTop: 10,
  },
  loginButtonText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
  },
  registerLink: {
    marginTop: 20,
    alignItems: 'center',
  },
  registerLinkText: {
    color: '#FF5722',
    fontSize: 14,
  },
});