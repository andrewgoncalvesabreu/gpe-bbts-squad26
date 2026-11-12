export interface DocumentoVersao {
  id: string
  numeroVersao: number
  nomeArquivo: string
  contentType: string
  tamanhoBytes: number
  hashSha256: string
  responsavelMatricula?: string | null
  dataEnvio: string
}

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
  parceriaId?: string | null
  parceriaNome?: string | null
  versaoAtual?: number | null
  nomeArquivoAtual?: string | null
  tamanhoArquivoAtual?: number | null
  statusAssinatura: string
  dataCriacao: string
  dataAtualizacao: string
  versao?: DocumentoVersao | null
}

export interface AssinaturaDocumento {
  id: string
  numeroVersao: number
  provedor: string
  status: string
  protocolo?: string | null
  detalhe?: string | null
  responsavelMatricula: string
  dataEvento: string
}
