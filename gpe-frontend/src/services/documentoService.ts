import { api } from './api'
import type { AssinaturaDocumento, Documento, DocumentoVersao } from '../types/documento'

export const documentoService = {
  listar: (parceiroId: string) =>
    api.get<Documento[]>(`/parceiros/${parceiroId}/documentos`).then((response) => response.data),
  anexar: (parceiroId: string, parceriaId: string, file: File, nome: string, descricao: string, responsavelMatricula: string) => {
    const form = new FormData()
    form.append('file', file)
    form.append('parceriaId', parceriaId)
    form.append('nome', nome)
    form.append('descricao', descricao)
    form.append('responsavelMatricula', responsavelMatricula)
    return api.post<Documento>(`/parceiros/${parceiroId}/documentos`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then((response) => response.data)
  },
  listarVersoes: (documentoId: string) =>
    api.get<DocumentoVersao[]>(`/documentos/${documentoId}/versoes`).then((response) => response.data),
  criarVersao: (documentoId: string, file: File, responsavelMatricula: string) => {
    const form = new FormData()
    form.append('file', file)
    form.append('responsavelMatricula', responsavelMatricula)
    return api.post<Documento>(`/documentos/${documentoId}/versoes`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then((response) => response.data)
  },
  baixar: (documentoId: string, version?: number) =>
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
    api.get<AssinaturaDocumento[]>(`/documentos/${documentoId}/assinaturas`).then((response) => response.data),
}
