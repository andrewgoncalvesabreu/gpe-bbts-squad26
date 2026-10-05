import axios from 'axios'
import type { ApiErrorBody } from '../types/parceiro'

export interface ParsedApiError {
  message: string
  fieldErrors: Record<string, string>
  status?: number
}

/** Converte qualquer erro do Axios no formato do GlobalExceptionHandler do backend. */
export function parseApiError(error: unknown): ParsedApiError {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    if (!error.response) {
      return { message: 'Não foi possível conectar ao servidor. Verifique se o back-end está no ar.', fieldErrors: {} }
    }
    const body = error.response.data
    return {
      message: body?.mensagem ?? 'Erro inesperado ao processar a requisição.',
      fieldErrors: body?.erros ?? {},
      status: error.response.status,
    }
  }
  return { message: 'Erro inesperado.', fieldErrors: {} }
}
