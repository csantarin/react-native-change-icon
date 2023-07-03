import { NativeModules } from "react-native";

/**
 * Swaps the currently active app icon for another one. The name provided must map to an icon that
 * exists as an asset in the app bundle in order for this method to work. Icons downloaded off the
 * cloud will not be applicable here.
 * @param {string | null} iconName Name of the icon to change to. `null` for primary icon on iOS.
 * @param {import('react-native-change-icon').ChangeIconOptions} changeIconOptions Additional options to alter the method behavior.
 * @returns {Promise<string | null>} Name of the icon to change to. `null` for primary icon on iOS.
 * @throws
 * - Android
 *   - `ACTIVITY_NOT_FOUND`
 *   - `UNEXPECTED_COMPONENT_CLASS`
 *   - `NULL_ICON_STRING`
 *   - `EMPTY_ICON_STRING`
 *   - `ICON_ALREADY_USED`
 *   - `SYSTEM_ERROR`
 * - iOS
 *   - `ALTERNATE_ICON_NOT_SUPPORTED`
 *   - `EMPTY_ICON_STRING`
 *   - `ICON_ALREADY_USED`
 *   - `SYSTEM_ERROR`
 */
const changeIcon = (iconName, changeIconOptions) => NativeModules.ChangeIcon.changeIcon(iconName, changeIconOptions).then((result) => result ?? null);

/**
 * Returns the current icon name or `null` if using primary icon on iOS.
 * @returns {Promise<string | null>} Name of the currently active icon.
 * @throws
 * - Android
 *   - `ACTIVITY_NOT_FOUND`
 *   - `UNEXPECTED_COMPONENT_CLASS`
 */
const getIcon = () => NativeModules.ChangeIcon.getIcon();

/**
 * Error codes when retrieving or swapping an icon name.
 */
const ERROR_CODES = Object.freeze({
  /**
   * Activity is inexplicably missing when retrieved.
   *
   * **Platforms:** Android
   */
  ACTIVITY_NOT_FOUND: "ACTIVITY_NOT_FOUND",
  /**
   * Current active class name is `MainActivity`, indicating likelihood of missing `<activity-alias>` setup.
   *
   * **Platforms:** Android
   */
  UNEXPECTED_COMPONENT_CLASS: "UNEXPECTED_COMPONENT_CLASS",
  /**
   * Option `whenToKillPrevActiveClass` was not given a recognized value.
   * 
   * **Platforms:** Android
   */
  INVALID_CLEANUP_CHECKPOINT: 'INVALID_CLEANUP_CHECKPOINT',
  /**
   * Alternate icon feature is not supported, indicating likelihood that no alternate icons was bundled.
   *
   * **Platform**: iOS
   */
  ALTERNATE_ICON_NOT_SUPPORTED: "ALTERNATE_ICON_NOT_SUPPORTED",
  /**
   * Icon name provided is `null` which is unusable when changing the currently active app icon.
   *
   * **Platforms:** Android
   */
  NULL_ICON_STRING: "NULL_ICON_STRING",
  /**
   * Icon name provided is `""` which is unusable when changing the currently active app icon.
   *
   * **Platforms**: Android, iOS
   */
  EMPTY_ICON_STRING: "EMPTY_ICON_STRING",
  /**
   * Icon name provided is cannot be set because it is currently active.
   *
   * **Platforms**: Android, iOS
   */
  ICON_ALREADY_USED: "ICON_ALREADY_USED",
  /**
   * Native API error where it is unable to proceed with changing the currently active app icon.
   *
   * **Platforms**: Android, iOS
   */
  SYSTEM_ERROR: "SYSTEM_ERROR",
});

export { changeIcon, ERROR_CODES, getIcon };
