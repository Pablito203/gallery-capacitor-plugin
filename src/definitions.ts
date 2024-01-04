export interface GalleryCapacitorPlugin {
  /**
   * Pick one or more images from the gallery.
   *
   * On iOS 13 and older it only allows to pick one image.
   *
   * Only available on Android and iOS.
   *
   * @since 0.5.3
   */
  pickFiles(options?: PickFilesOptions): Promise<PickFilesResult>;
}

export interface PickFilesOptions {
  /**
   * Max files to be selected
   *
   * @default 15
   */
  maximumFilesCount?: number;
}

export interface PickFilesResult {
  files: PickedFile[];
}

export interface PickedFile {
  /**
   * The mime type of the file.
   */
  mimeType: string;
  /**
   * The name of the file.
   */
  name: string;
  /**
   * The path of the file.
   *
   * Only available on Android and iOS.
   */
  path?: string;
  /**
   * The size of the file in bytes.
   */
  size: number;
}
