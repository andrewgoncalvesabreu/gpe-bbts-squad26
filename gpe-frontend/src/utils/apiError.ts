import axios from 'axios'
import type { ApiErrorBody } from '../types/parceiro'

export interface ParsedApiError {
  message: string
  fieldErrors: Record<string, string>
  status?: number
}

export function parseApiError(error: unknown): ParsedApiError {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    if (!error.response) {
      return {
        message: 'Não foi possível conectar ao servidor. Verifique se o back-end está no ar.',
        fieldErrors: {},
      }
    }

    const body = error.response.data

    if (typeof body === 'string') {
      return {
        message: body,
        fieldErrors: {},
        status: error.response.status,
      }
    }

    return {
      message:
        body?.mensagem ??
        body?.mensagem ??
        `Erro HTTP ${error.response.status} ao processar a requisição.`,
      fieldErrors: body?.erros ?? {},
      status: error.response.status,
    }
  }

  if (error instanceof Error) {
    return {
      message: error.message,
      fieldErrors: {},
    }
  }

  return {
    message: 'Erro inesperado.',
    fieldErrors: {},
  }
}

