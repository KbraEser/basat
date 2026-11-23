import type { ApiResponse, ProblemDetail } from '@/types/api'

/** Thrown for every answer that is not 2xx; keeps the HTTP status and the backend's problem detail. */
export class ApiError extends Error {
  readonly status: number
  readonly problem?: ProblemDetail

  constructor(status: number, problem?: ProblemDetail) {
    super(problem?.detail ?? problem?.title ?? `Request failed with status ${status}`)
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
  }
}

let refreshing: Promise<boolean> | null = null

function refreshSession(): Promise<boolean> {
  refreshing ??= fetch('/api/auth/refresh', { method: 'POST' })
    .then((response) => response.ok)
    .catch(() => false)
    .finally(() => {
      refreshing = null
    })
  return refreshing
}


async function request<T>(method: string, path: string, body?: unknown, retried = false): Promise<T> {
  const response = await fetch(path, {
    method,
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if(response.status === 401 && !retried && !path.startsWith('/api/auth/')) {
    if(await refreshSession()){
        return request<T>(method, path, body, true)
    }
  }

  if (!response.ok) {
    // error bodies are problem details; a proxy error page may not be JSON at all
    const problem = (await response.json().catch(() => undefined)) as ProblemDetail | undefined
    throw new ApiError(response.status, problem)
  }
  // 204 No Content (e.g. logout) has no body to read
  if (response.status === 204) {
    return undefined as T
  }
  const envelope = (await response.json()) as ApiResponse<T>
  return envelope.data
}

/** Small wrapper around fetch: sends JSON, unwraps ApiResponse.data and turns errors into ApiError. */
export const api = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, body),
  put: <T>(path: string, body: unknown) => request<T>('PUT', path, body),
  delete: (path: string) => request<void>('DELETE', path),
}