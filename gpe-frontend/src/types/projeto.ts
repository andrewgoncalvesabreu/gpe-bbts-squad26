import type { InstrumentoJuridico } from './instrumento'
import type { Parceiro } from './parceiro'

export interface Parceria {
  id: string
  nome: string
  parceiroId: string
  parceiroNome: string
  instrumentoId: string
  instrumentoNome: string
  status: string
  dataCriacao: string
}

export interface ParceriaPayload {
  nome: string
  parceiroId: string
  instrumentoId: string
}

export interface Projeto {
  id: string
  parceriaId: string
  parceriaNome: string
  parceiroNome: string
  nome: string
  descricao: string
  status: string
  dataCriacao: string
  possuiPlanoTrabalho: boolean
}

export interface ProjetoPayload {
  parceriaId: string
  nome: string
  descricao: string
}

export interface EntregaPlano {
  id?: string
  descricao: string
  dataPrevista: string
  responsavel: string
}

export interface MarcoPlano {
  id?: string
  descricao: string
  dataPrevista: string
}

export interface PlanoTrabalho {
  id: string
  projetoId: string
  objetivos: string[]
  cronogramaInicio: string
  cronogramaFim: string
  responsaveis: string
  entregas: EntregaPlano[]
  marcos: MarcoPlano[]
  dataCriacao: string
}

export type PlanoTrabalhoPayload = Omit<PlanoTrabalho, 'id' | 'projetoId' | 'dataCriacao'>

export type ParceiroResumo = Pick<Parceiro, 'id' | 'razaoSocial' | 'status'>
export type InstrumentoResumo = Pick<InstrumentoJuridico, 'id' | 'nome' | 'parceiro'>
