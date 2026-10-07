import type { Parceiro, ParceiroPayload } from '../types/parceiro'
import { maskCep, onlyDigits } from './format'

/**
 * O backend guarda endereço, responsáveis e áreas de atuação em campos de texto único.
 * O protótipo, porém, tem campos separados. Este arquivo converte nos dois sentidos.
 */

export interface ParceiroFormValues {
  razaoSocial: string
  nomeFantasia: string
  cnpj: string // com máscara na tela
  tipoOrganizacao: string
  porte: string
  status: string
  // Endereço
  cep: string
  logradouro: string
  numero: string
  complemento: string
  cidade: string
  uf: string
  pais: string
  // Contato principal
  responsavelNome: string
  responsavelCargo: string
  emailContato: string
  telefone: string
  // Áreas
  areas: string[]
}

export const emptyForm: ParceiroFormValues = {
  razaoSocial: '',
  nomeFantasia: '',
  cnpj: '',
  tipoOrganizacao: '',
  porte: '',
  status: 'EM_ANALISE',
  cep: '',
  logradouro: '',
  numero: '',
  complemento: '',
  cidade: '',
  uf: '',
  pais: 'Brasil',
  responsavelNome: '',
  responsavelCargo: '',
  emailContato: '',
  telefone: '',
  areas: [],
}

// "Av. Antônio Carlos, 6627, Sala 1 - Belo Horizonte/MG - CEP 31270-901 - Brasil"
export function formatEndereco(v: ParceiroFormValues): string {
  const rua = [v.logradouro, v.numero, v.complemento].filter(Boolean).join(', ')
  const cidadeUf = v.cidade && v.uf ? `${v.cidade}/${v.uf}` : v.cidade || v.uf
  return [rua, cidadeUf, v.cep ? `CEP ${v.cep}` : '', v.pais].filter(Boolean).join(' - ')
}

export function parseEndereco(texto?: string | null) {
  const vazio = { logradouro: '', numero: '', complemento: '', cidade: '', uf: '', cep: '', pais: 'Brasil' }
  if (!texto) return vazio
  const m = texto.match(/^(.*?), (.*?)(?:, (.*?))? - (.*?)\/(\w{2}) - CEP (.*?) - (.*)$/)
  if (!m) return { ...vazio, logradouro: texto } // endereço digitado fora do padrão: preserva o texto
  return {
    logradouro: m[1],
    numero: m[2],
    complemento: m[3] ?? '',
    cidade: m[4],
    uf: m[5],
    cep: maskCep(m[6]),
    pais: m[7],
  }
}

// "Maria Souza (Diretora de P&D)"
export function formatResponsavel(nome: string, cargo: string): string {
  return cargo ? `${nome} (${cargo})` : nome
}

export function parseResponsavel(texto?: string | null) {
  if (!texto) return { nome: '', cargo: '' }
  const m = texto.match(/^(.*) \((.*)\)$/)
  return m ? { nome: m[1], cargo: m[2] } : { nome: texto, cargo: '' }
}

export function parceiroToForm(p: Parceiro): ParceiroFormValues {
  const end = parseEndereco(p.enderecoCompleto)
  const resp = parseResponsavel(p.responsaveis)
  return {
    razaoSocial: p.razaoSocial,
    nomeFantasia: p.nomeFantasia ?? '',
    cnpj: p.cnpj,
    tipoOrganizacao: p.tipoOrganizacao,
    porte: p.porte ?? '',
    status: p.status,
    ...end,
    responsavelNome: resp.nome,
    responsavelCargo: resp.cargo,
    emailContato: p.emailContato ?? '',
    telefone: p.telefone ?? '',
    areas: p.segmentoAtuacao ? p.segmentoAtuacao.split(',').map((a) => a.trim()).filter(Boolean) : [],
  }
}

export function formToPayload(v: ParceiroFormValues): ParceiroPayload {
  return {
    razaoSocial: v.razaoSocial.trim(),
    nomeFantasia: v.nomeFantasia.trim() || null,
    cnpj: v.cnpj
    .toUpperCase()
    .replace(/[^0-9A-Z]/g, ''),
    tipoOrganizacao: v.tipoOrganizacao as ParceiroPayload['tipoOrganizacao'],
    segmentoAtuacao: v.areas.join(', ') || null,
    porte: v.porte || null,
    emailContato: v.emailContato.trim() || null,
    telefone: v.telefone.trim() || null,
    enderecoCompleto: formatEndereco(v) || null,
    responsaveis: formatResponsavel(v.responsavelNome.trim(), v.responsavelCargo.trim()) || null,
    status: v.status || 'EM_ANALISE', // obrigatório no backend (@NotBlank)
  }
}
