// src/utils/waterReminders.ts
import notifee, { TriggerType, RepeatFrequency } from '@notifee/react-native';

export async function scheduleWaterReminders() {
  const channelId = await notifee.createChannel({
    id: 'water', name: 'Przypomnienia o wodzie',
  });

  const hours = [10, 14, 18];
  for (const hour of hours) {
    const date = new Date();
    date.setHours(hour, 0, 0, 0);
    if (date < new Date()) date.setDate(date.getDate() + 1);

    await notifee.createTriggerNotification(
      { title: '💧 Czas na wodę!', body: 'Pamiętaj o nawodnieniu.', android: { channelId } },
      { type: TriggerType.TIMESTAMP, timestamp: date.getTime(), repeatFrequency: RepeatFrequency.DAILY }
    );
  }
}