import { api } from './api'
import type { Documento, DocumentoVersao } from '../types/documento'

export const documentoService = {
  listar: (params?: {
    parceiroId?: string
    oportunidadeId?: string
    instrumentoId?: string
  }) =>
    api
      .get<Documento[]>('/documentos', { params })
      .then((response) => response.data),

  criar: (dados: FormData) =>
    api
      .post<Documento>('/documentos', dados)
      .then((response) => response.data),

  listarVersoes: (id: string) =>
    api
      .get<DocumentoVersao[]>(`/documentos/${id}/versoes`)
      .then((response) => response.data),

  adicionarVersao: (id: string, arquivo: File) => {
    const formData = new FormData()
    formData.append('arquivo', arquivo)

    return api
      .post<DocumentoVersao>(
        `/documentos/${id}/versoes`,
        formData,
      )
      .then((response) => response.data)
  },

  baixar: async (
    id: string,
    versao: number,
    nomeArquivo: string,
  ) => {
    const response = await api.get(
      `/documentos/${id}/versoes/${versao}/download`,
      {
        responseType: 'blob',
      },
    )

    const url = URL.createObjectURL(response.data)
    const anchor = document.createElement('a')

    anchor.href = url
    anchor.download = nomeArquivo
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()

    URL.revokeObjectURL(url)
  },
}
