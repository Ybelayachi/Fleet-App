import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';
import { Readable } from 'node:stream';

const browserDistFolder = join(import.meta.dirname, '../browser');
const backendUrl = process.env['BACKEND_URL'] || 'http://localhost:8080';

const app = express();
const angularApp = new AngularNodeAppEngine();

app.use('/api', async (req, res, next) => {
  try {
    const targetUrl = new URL(req.originalUrl, backendUrl).toString();
    const requestHeaders = new Headers();

    Object.entries(req.headers).forEach(([key, value]) => {
      if (value === undefined) {
        return;
      }
      if (Array.isArray(value)) {
        requestHeaders.set(key, value.join(','));
      } else {
        requestHeaders.set(key, value);
      }
    });

    requestHeaders.set('host', new URL(backendUrl).host);

    const isBodyAllowed = req.method !== 'GET' && req.method !== 'HEAD';
    const response = await fetch(targetUrl, {
      method: req.method,
      headers: requestHeaders,
      body: isBodyAllowed ? (req as unknown as BodyInit) : undefined,
      duplex: 'half',
    } as RequestInit & { duplex: 'half' });

    res.status(response.status);
    response.headers.forEach((value, key) => {
      if (key.toLowerCase() === 'transfer-encoding') {
        return;
      }
      res.setHeader(key, value);
    });

    if (response.body) {
      Readable.fromWeb(response.body as unknown as import('node:stream/web').ReadableStream).pipe(res);
      return;
    }

    res.end();
  } catch (error) {
    console.error(`API proxy error for ${req.method} ${req.originalUrl}:`, error);
    if (!res.headersSent) {
      res.status(502).json({
        error: 'backend_unreachable',
        message: 'Unable to reach backend service. Check BACKEND_URL or backend server status.',
      });
      return;
    }
    next(error);
  }
});

/**
 * Serve static files from /browser
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * Start the server if this module is the main entry point, or it is ran via PM2.
 * The server listens on the port defined by the `PORT` environment variable, or defaults to 4000.
 */
if (isMainModule(import.meta.url) || process.env['pm_id']) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
 */
export const reqHandler = createNodeRequestHandler(app);
