import { api } from './api'
import type { AssinaturaDocumento, Documento, DocumentoVersao } from '../types/documento'

export const documentoService = {
  listar: (params?: {
    parceiroId?: string
    oportunidadeId?: string
    instrumentoId?: string
    parceriaId?: string
  }) => api.get<Documento[]>('/documentos', { params }).then((response) => response.data),

  listarPorParceiro: (parceiroId: string) =>
    api.get<Documento[]>(`/parceiros/${parceiroId}/documentos`).then((response) => response.data),

  criar: (dados: FormData) =>
    api.post<Documento>('/documentos', dados).then((response) => response.data),

  anexar: (
    parceiroId: string,
    parceriaId: string,
    file: File,
    nome: string,
    descricao: string,
    responsavelMatricula: string,
  ) => {
    const form = new FormData()
    form.append('file', file)
    form.append('parceriaId', parceriaId)
    form.append('nome', nome)
    form.append('descricao', descricao)
    form.append('responsavelMatricula', responsavelMatricula)
    return api.post<Documento>(`/parceiros/${parceiroId}/documentos`, form).then((response) => response.data)
  },

  listarVersoes: (documentoId: string) =>
    api.get<DocumentoVersao[]>(`/documentos/${documentoId}/versoes`).then((response) => response.data),

  adicionarVersao: (documentoId: string, arquivo: File, responsavelMatricula?: string) => {
    const form = new FormData()
    form.append('arquivo', arquivo)
    if (responsavelMatricula) form.append('responsavelMatricula', responsavelMatricula)
    return api.post<DocumentoVersao>(`/documentos/${documentoId}/versoes`, form)
      .then((response) => response.data)
  },

  baixar: async (id: string, versao: number, nomeArquivo: string) => {
    const response = await api.get<Blob>(`/documentos/${id}/versoes/${versao}/download`, {
      responseType: 'blob',
    })
    const url = URL.createObjectURL(response.data)
    const anchor = window.document.createElement('a')
    anchor.href = url
    anchor.download = nomeArquivo
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
    URL.revokeObjectURL(url)
  },

  baixarBlob: (documentoId: string, version?: number) =>
    api.get<Blob>(
      version === undefined
        ? `/documentos/${documentoId}`
        : `/documentos/${documentoId}/versoes/${version}/arquivo`,
      { responseType: 'blob' },
    ).then((response) => response.data),

  enviarAssinatura: (documentoId: string, responsavelMatricula: string) =>
    api.post<AssinaturaDocumento>(`/documentos/${documentoId}/assinaturas`, { responsavelMatricula })
      .then((response) => response.data),

  listarAssinaturas: (documentoId: string) =>
    api.get<AssinaturaDocumento[]>(`/documentos/${documentoId}/assinaturas`)
      .then((response) => response.data),
}
