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
    /**
       * Check read media permission.
       * Required on Android only in android.
       *
       * @since 1.0.0
       */
    checkPermissions(): Promise<PermissionStatus>;
    /**
     * Request read media permission.
     * Required on Android only in android..
     *
     * @since 1.0.0
     */
    requestPermissions(): Promise<PermissionStatus>;
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
export declare type GalleryPermissionState = 'granted' | 'limited' | 'denied';
export declare const GalleryPermissionType: {
    /**
     * The permission name for Android 13 / API 33 and above.
     */
    TIRAMISU_GALLERY: string;
    /**
     * The permission name for Android 12 / API 32 and below.
     */
    GALLERY: string;
};
export interface PermissionStatus {
    gallery?: GalleryPermissionState;
    tiramisuGallery?: GalleryPermissionState;
}
