/**
 * Centralized API client using fetch().
 * Connects to the Core Java backend REST endpoints on http://localhost:8080/api.
 */

const API_BASE = 'http://localhost:8080/api';

async function handleResponse(res) {
  if (!res.ok) {
    let errorMsg = `HTTP Error ${res.status}`;
    try {
      const data = await res.json();
      if (data && data.error) errorMsg = data.error;
    } catch (e) {
      // Body not JSON
    }
    throw new Error(errorMsg);
  }
  if (res.status === 204) return null;
  return res.json();
}

// Notes API
export async function getNotes(params = {}) {
  const query = new URLSearchParams();
  if (params.q) query.set('q', params.q);
  if (params.folderId) query.set('folderId', params.folderId);
  if (params.tag) query.set('tag', params.tag);
  if (params.sort) query.set('sort', params.sort);
  if (params.order) query.set('order', params.order);

  const url = `${API_BASE}/notes${query.toString() ? '?' + query.toString() : ''}`;
  const res = await fetch(url);
  return handleResponse(res);
}

export async function getRecentNotes() {
  const res = await fetch(`${API_BASE}/notes/recent`);
  return handleResponse(res);
}

export async function getNoteById(id) {
  const res = await fetch(`${API_BASE}/notes/${id}`);
  return handleResponse(res);
}

export async function createNote(noteData) {
  const res = await fetch(`${API_BASE}/notes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(noteData),
  });
  return handleResponse(res);
}

export async function updateNote(id, noteData) {
  const res = await fetch(`${API_BASE}/notes/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(noteData),
  });
  return handleResponse(res);
}

export async function deleteNote(id) {
  const res = await fetch(`${API_BASE}/notes/${id}`, {
    method: 'DELETE',
  });
  return handleResponse(res);
}

export async function togglePin(id) {
  const res = await fetch(`${API_BASE}/notes/${id}/pin`, {
    method: 'PATCH',
  });
  return handleResponse(res);
}

// Folders API
export async function getFolders() {
  const res = await fetch(`${API_BASE}/folders`);
  return handleResponse(res);
}

export async function createFolder(name) {
  const res = await fetch(`${API_BASE}/folders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name }),
  });
  return handleResponse(res);
}

export async function updateFolder(id, name) {
  const res = await fetch(`${API_BASE}/folders/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name }),
  });
  return handleResponse(res);
}

export async function deleteFolder(id) {
  const res = await fetch(`${API_BASE}/folders/${id}`, {
    method: 'DELETE',
  });
  return handleResponse(res);
}

// Tags API
export async function getTags() {
  const res = await fetch(`${API_BASE}/tags`);
  return handleResponse(res);
}
