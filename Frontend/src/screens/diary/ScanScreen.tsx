// // src/screens/diary/ScanScreen.tsx
// import { Camera, useCameraDevice, useCodeScanner } from 'react-native-vision-camera';

// export default function ScanScreen({ navigation }) {
//   const device = useCameraDevice('back');

//   const codeScanner = useCodeScanner({
//     codeTypes: ['ean-13', 'ean-8'],
//     onCodeScanned: codes => {
//       const barcode = codes[0]?.value;
//       if (barcode) navigation.navigate('Search', { barcode });
//     },
//   });

//   if (!device) return <Text>Brak aparatu</Text>;

//   return (
//     <Camera
//       style={StyleSheet.absoluteFill}
//       device={device}
//       isActive={true}
//       codeScanner={codeScanner}
//     />
//   );
// }