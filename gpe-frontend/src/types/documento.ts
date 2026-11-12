export interface DocumentoVersao {
  id: string
  numeroVersao: number
  nomeArquivo: string
  tipoMime: string
  tamanhoBytes: number
  sha256: string
  responsavelMatricula: string
  dataUpload: string
}

export interface Documento {
  id: string
  parceiroId: string
  parceiroNome: string
  parceriaId: string | null
  parceriaNome: string | null
  nome: string
  descricao?: string | null
  versaoAtual: number
  statusAssinatura: string
  dataCriacao: string
  versao: DocumentoVersao
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
