export interface Auditoria {
  id: string
  usuario: string
  operacao: string
  entidade: string
  entidadeId?: string | null
  dataHora: string
  conteudoAnterior?: string | null
  conteudoNovo?: string | null
  ipOrigem?: string | null
}
