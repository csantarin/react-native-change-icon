declare module 'react-native-change-icon' {
  /**
   * Swaps the currently active app icon for another one. The name provided must map to an icon that
   * exists as an asset in the app bundle in order for this method to work. Icons downloaded off the
   * cloud will not be applicable here.
   * @param iconName Name of the icon to change to. `null` for primary icon on iOS.
   * @returns Name of the icon to change to. `null` for primary icon on iOS.
   */
  const changeIcon: (iconName: string | null) => Promise<string | null>;

  /**
   * Returns the current icon name or `null` if using primary icon on iOS.
   * @returns Name of the currently active icon.
   */
  const getIcon: () => Promise<string | null>;

  /** **iOS only** error codes when changing an icon. */
  const ChangeIconErrorCode: {
    notSupported: string;
    alreadyInUse: string;
    systemError: string;
  };
}
