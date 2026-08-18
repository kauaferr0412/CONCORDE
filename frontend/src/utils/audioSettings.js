const INPUT_KEY = "audioInputDeviceId";
const OUTPUT_KEY = "audioOutputDeviceId";
const SOUND_EFFECTS_KEY = "voiceSoundEffectsEnabled";

export function getSavedAudioInput() {
  return localStorage.getItem(INPUT_KEY) || "";
}
export function setSavedAudioInput(deviceId) {
  if (deviceId) localStorage.setItem(INPUT_KEY, deviceId);
  else localStorage.removeItem(INPUT_KEY);
}

export function getSavedAudioOutput() {
  return localStorage.getItem(OUTPUT_KEY) || "";
}
export function setSavedAudioOutput(deviceId) {
  if (deviceId) localStorage.setItem(OUTPUT_KEY, deviceId);
  else localStorage.removeItem(OUTPUT_KEY);
}

/** Som ao alguem entrar/sair da call - ligado por padrao, como no Discord. */
export function getSoundEffectsEnabled() {
  const raw = localStorage.getItem(SOUND_EFFECTS_KEY);
  return raw === null ? true : raw === "true";
}
export function setSoundEffectsEnabled(enabled) {
  localStorage.setItem(SOUND_EFFECTS_KEY, String(enabled));
}
