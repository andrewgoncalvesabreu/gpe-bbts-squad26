import type { Parceiro } from './parceiro'

export type TipoInstrumento =
  | 'ACORDO_COOPERACAO'
  | 'CONVENIO'
  | 'CONTRATO'
  | 'TERMO_COOPERACAO'
  | 'TERMO_PARCERIA'
  | 'PROTOCOLO_INTENCOES'
  | 'OUTRO'

export interface OportunidadeResumo {
  id: string
  titulo: string
  parceiro: Pick<Parceiro, 'id'>
}

export interface InstrumentoJuridico {
  id: string
  nome: string
  tipo: TipoInstrumento
  numeroInstrumento?: string | null
  descricao?: string | null
  dataInicioVigencia: string
  dataFimVigencia: string
  parceiro: Pick<Parceiro, 'id' | 'razaoSocial'>
  oportunidade?: OportunidadeResumo | null
  dataCadastro: string
}

export interface InstrumentoPayload {
  nome: string
  tipo: TipoInstrumento
  numeroInstrumento?: string | null
  descricao?: string | null
  dataInicioVigencia: string
  dataFimVigencia: string
  parceiro: { id: string }
  oportunidade?: { id: string } | null
}

export interface ProrrogacaoInstrumento {
  id: string
  dataFimAnterior: string
  dataFimNova: string
  justificativa?: string | null
  dataProrrogacao: string
}

export interface InstrumentoAlerta {
  instrumentoId: string
  instrumentoNome: string
  parceiroNome: string
  dataVencimento: string
  diasAteVencimento: number
}
