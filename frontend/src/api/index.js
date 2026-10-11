import { request, requestBlob } from './client.js';

function query(params) {
  const search = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '' && value !== 'all') {
      search.append(key, value);
    }
  });
  const string = search.toString();
  return string ? `?${string}` : '';
}

export const api = {
  projects: {
    // Public, but sending the token lets the API report whether the viewer has joined.
    list: (params) => request(`/api/projects${query(params)}`),
    get: (id) => request(`/api/projects/${id}`),
    join: (id, participantRole) =>
      request(`/api/projects/${id}/participants`, { method: 'POST', body: { participantRole } }),
    addUpdate: (id, payload) =>
      request(`/api/projects/${id}/updates`, { method: 'POST', body: payload }),
  },

  alerts: {
    list: () => request('/api/alerts'),
    unreadCount: () => request('/api/alerts/unread-count'),
    markRead: (id) => request(`/api/alerts/${id}/read`, { method: 'POST' }),
    markAllRead: () => request('/api/alerts/read-all', { method: 'POST' }),
  },

  admin: {
    users: (search) => request(`/api/admin/users${query({ query: search })}`),
    setSuspension: (id, suspended, reason) =>
      request(`/api/admin/users/${id}/suspension`, { method: 'POST', body: { suspended, reason } }),
    verifications: (status) => request(`/api/admin/verifications${query({ status })}`),
    reviewAccount: (id, approved, reason) =>
      request(`/api/admin/verifications/${id}`, { method: 'POST', body: { approved, reason } }),
    document: (id) => requestBlob(`/api/admin/documents/${id}`),
  },
};
