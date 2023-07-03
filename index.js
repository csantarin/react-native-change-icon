import { NativeModules } from "react-native";

/**
 * Swaps the currently active app icon for another one. The name provided must map to an icon that
 * exists as an asset in the app bundle in order for this method to work. Icons downloaded off the
 * cloud will not be applicable here.
 * @param {string | null} iconName Name of the icon to change to. `null` for primary icon on iOS.
 * @returns {Promise<string | null>} Name of the icon to change to. `null` for primary icon on iOS.
 */
const changeIcon = (iconName) => NativeModules.ChangeIcon.changeIcon(iconName).then((result) => result ?? null);

/**
 * Returns the current icon name or `null` if using primary icon on iOS or if icon name is empty
 * inexplicably on Android.
 * @returns {Promise<string | null>} Name of the currently active icon.
 */
const getIcon = () => NativeModules.ChangeIcon.getIcon();

/** **iOS only** error codes when changing an icon. */
const ChangeIconErrorCode = {
  notSupported: "NOT_SUPPORTED",
  alreadyInUse: "ALREADY_IN_USE",
  systemError: "SYSTEM_ERROR",
}

export { changeIcon, ChangeIconErrorCode, getIcon };
