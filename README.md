# gallery-capacitor-plugin

Get files from device storage

## Install

```bash
npm install gallery-capacitor-plugin
npx cap sync
```

## API

<docgen-index>

* [`pickFiles(...)`](#pickfiles)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### pickFiles(...)

```typescript
pickFiles(options?: PickFilesOptions | undefined) => Promise<PickFilesResult>
```

Pick one or more images from the gallery.

On iOS 13 and older it only allows to pick one image.

Only available on Android and iOS.

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#pickfilesoptions">PickFilesOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#pickfilesresult">PickFilesResult</a>&gt;</code>

**Since:** 0.5.3

--------------------


### Interfaces


#### PickFilesResult

| Prop        | Type                      |
| ----------- | ------------------------- |
| **`files`** | <code>PickedFile[]</code> |


#### PickedFile

| Prop           | Type                | Description                                              |
| -------------- | ------------------- | -------------------------------------------------------- |
| **`mimeType`** | <code>string</code> | The mime type of the file.                               |
| **`name`**     | <code>string</code> | The name of the file.                                    |
| **`path`**     | <code>string</code> | The path of the file. Only available on Android and iOS. |
| **`size`**     | <code>number</code> | The size of the file in bytes.                           |


#### PickFilesOptions

| Prop                    | Type                | Description              | Default         |
| ----------------------- | ------------------- | ------------------------ | --------------- |
| **`maximumFilesCount`** | <code>number</code> | Max files to be selected | <code>15</code> |

</docgen-api>
