const { app, BrowserWindow, protocol } = require("electron");
const path = require("path");

const APP_PROTOCOL = "concorde";
const DEV_URL = "http://localhost:5173";

let mainWindow;

function createWindow(deepLinkUrl) {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    title: "Concorde",
    webPreferences: {
      // getUserMedia/getDisplayMedia (mic, camera, compartilhar tela) funcionam
      // normalmente aqui, igual em um Chrome comum.
      contextIsolation: true,
      nodeIntegration: false,
    },
  });

  const startUrl = app.isPackaged
    ? `file://${path.join(__dirname, "../dist/index.html")}`
    : DEV_URL;

  mainWindow.loadURL(startUrl);

  if (deepLinkUrl) {
    routeDeepLink(deepLinkUrl);
  }
}

/** concorde://invite/<code>  ->  /invite/<code> dentro do React Router */
function routeDeepLink(url) {
  if (!mainWindow) return;
  const match = url.match(/^concorde:\/\/invite\/(.+)$/);
  if (match) {
    const code = match[1];
    const target = app.isPackaged
      ? `file://${path.join(__dirname, "../dist/index.html")}#/invite/${code}`
      : `${DEV_URL}/#/invite/${code}`;
    mainWindow.loadURL(target);
  }
}

// Registra o protocolo concorde:// no sistema operacional
if (!app.isDefaultProtocolClient(APP_PROTOCOL)) {
  app.setAsDefaultProtocolClient(APP_PROTOCOL);
}

// Garante uma unica instancia (necessario para deep link funcionar bem no Windows)
const gotLock = app.requestSingleInstanceLock();
if (!gotLock) {
  app.quit();
} else {
  app.on("second-instance", (_event, argv) => {
    const deepLink = argv.find((arg) => arg.startsWith(`${APP_PROTOCOL}://`));
    if (mainWindow) {
      if (mainWindow.isMinimized()) mainWindow.restore();
      mainWindow.focus();
    }
    if (deepLink) routeDeepLink(deepLink);
  });

  app.whenReady().then(() => {
    const deepLink = process.argv.find((arg) => arg.startsWith(`${APP_PROTOCOL}://`));
    createWindow(deepLink);
  });
}

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});
