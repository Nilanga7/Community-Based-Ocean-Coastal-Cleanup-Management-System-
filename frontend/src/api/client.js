// A trailing slash would produce "//api/..." paths.
const BASE_URL = (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');

/** Uploaded files are served by the API; prefix its origin when the frontend is hosted elsewhere. */
export const mediaUrl = (url) => (url && url.startsWith('/api/') ? `${BASE_URL}${url}` : url);
const TOKEN_KEY = 'tideline.token';
const UNREACHABLE = 'Cannot reach the server — check that the API is running on port 8080.';

export class ApiError extends Error {
  constructor(message, status, code) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

/** "Remember me" keeps the session after the browser closes; otherwise it ends with the browser session. */
export function setToken(token, remember = true) {
  try {
    clearToken();
    (remember ? localStorage : sessionStorage).setItem(TOKEN_KEY, token);
  } catch {
    /* a blocked store just means the session ends when the tab closes */
  }
}

export function clearToken() {
  try {
    localStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(TOKEN_KEY);
  } catch {
    /* nothing to clear */
  }
}

const FALLBACK_MESSAGE = {
  401: 'Your session has expired — sign in again.',
  403: 'You do not have permission to do that.',
  404: 'That item no longer exists.',
  413: 'Those files are too large. Each file must be 5 MB or smaller.',
  500: 'The server could not complete that request. Try again in a moment.',
};

function authHeaders(auth) {
  const token = auth ? getToken() : null;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

function failure(response, payload) {
  const message = payload?.message
    || FALLBACK_MESSAGE[response.status]
    || `Request failed (${response.status}).`;
  return new ApiError(message, response.status, payload?.code);
}

export async function request(path, { method = 'GET', body, auth = true } = {}) {
  // FormData sets its own multipart boundary, so it must not get a JSON content type.
  const isForm = body instanceof FormData;
  const headers = authHeaders(auth);
  if (body !== undefined && !isForm) {
    headers['Content-Type'] = 'application/json';
  }

  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : isForm ? body : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(UNREACHABLE, 0);
  }

  const text = await response.text();
  let payload = null;
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = null;
    }
  }

  if (!response.ok) {
    throw failure(response, payload);
  }

  return payload;
}

/** For files that need the sign-in token, such as uploaded certificates. */
export async function requestBlob(path) {
  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, { headers: authHeaders(true) });
  } catch {
    throw new ApiError(UNREACHABLE, 0);
  }
  if (!response.ok) {
    let payload = null;
    try {
      payload = await response.json();
    } catch {
      payload = null;
    }
    throw failure(response, payload);
  }
  return response.blob();
}
