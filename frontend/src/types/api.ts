// Every successful response from the backend is wrapped in this envelope (ApiResponse<T> in Java).
export interface ApiResponse<T> {
  data: T
  message?: string
  timestamp: string
}

// Errors follow RFC 9457 (ProblemDetail in Spring). `errors` lists invalid fields on a 400.
export interface ProblemDetail {
  title: string
  status: number
  detail?: string
  errors?: Record<string, string>
}
