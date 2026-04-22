import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'login',
    renderMode: RenderMode.Server
  },
  {
    // All protected routes rendered client-side so auth guards
    // run in the browser with localStorage access (SSR has no localStorage)
    path: '**',
    renderMode: RenderMode.Client
  }
];
