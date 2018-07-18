# iTools SMS Viewer

> SMS viewer for iTools Super Backup

### Preview

<img src="https://rawgit.com/valsaven/itools-sms-viewer/master/preview.png" alt="preview" width="300">

### Build Setup

``` bash
# install dependencies
npm i

# resolve sqlite3 problems
npm install sqlite3 --build-from-source
./node_modules/.bin/electron-rebuild -w sqlite3 -p

# serve with hot reload at localhost:9080
npm run dev

# build electron application for production
npm run build


# lint all JS/Vue component files in `src/`
npm run lint

```

### Have tested with:

Apple iPhone 4 A1332 32 GB (GSM)
