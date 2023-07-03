declare module 'react-native-change-icon' {
  /**
   * Swaps the currently active app icon for another one. The name provided must map to an icon that
   * exists as an asset in the app bundle in order for this method to work. Icons downloaded off the
   * cloud will not be applicable here.
   * @param iconName Name of the icon to change to. `null` for primary icon on iOS.
   * @returns Name of the icon to change to. `null` for primary icon on iOS.
   * @throws
   * - Android
   *   - `ACTIVITY_NOT_FOUND`
   *   - `UNEXPECTED_COMPONENT_CLASS`
   *   - `NULL_ICON_STRING`
   *   - `EMPTY_ICON_STRING`
   *   - `ICON_ALREADY_USED`
   *   - `SYSTEM_ERROR`
   * - iOS
   *   - `NOT_SUPPORTED`
   *   - `EMPTY_ICON_STRING`
   *   - `ICON_ALREADY_USED`
   *   - `SYSTEM_ERROR`
   */
  const changeIcon: (iconName: string | null) => Promise<string | null>;

  /**
   * Returns the current icon name or `null` if using primary icon on iOS.
   * @returns Name of the currently active icon.
   * @throws
   * - Android
   *   - `ACTIVITY_NOT_FOUND`
   *   - `UNEXPECTED_COMPONENT_CLASS`
   */
  const getIcon: () => Promise<string | null>;

  /** **iOS only** error codes when changing an icon. */
  const ChangeIconErrorCode: {
    notSupported: string;
    alreadyInUse: string;
    systemError: string;
  };
}
