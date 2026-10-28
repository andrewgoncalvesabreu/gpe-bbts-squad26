export interface Documento {
  id: string
  nome: string
  descricao?: string | null
  parceiroId?: string | null
  parceiroNome?: string | null
  oportunidadeId?: string | null
  oportunidadeTitulo?: string | null
  instrumentoId?: string | null
  instrumentoNome?: string | null
  versaoAtual?: number | null
  nomeArquivoAtual?: string | null
  tamanhoArquivoAtual?: number | null
  dataAtualizacao: string
}
export interface DocumentoVersao {
  id: string
  numeroVersao: number
  nomeArquivo: string
  contentType: string
  tamanhoBytes: number
  hashSha256: string
  dataEnvio: string
}