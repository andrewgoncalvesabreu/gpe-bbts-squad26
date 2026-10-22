import type { TipoInstrumento } from '../types/instrumento'

export const TIPO_INSTRUMENTO_OPTIONS: { label: string; value: TipoInstrumento }[] = [
  { label: 'Acordo de Cooperação', value: 'ACORDO_COOPERACAO' },
  { label: 'Convênio', value: 'CONVENIO' },
  { label: 'Contrato', value: 'CONTRATO' },
  { label: 'Termo de Cooperação', value: 'TERMO_COOPERACAO' },
  { label: 'Termo de Parceria', value: 'TERMO_PARCERIA' },
  { label: 'Protocolo de Intenções', value: 'PROTOCOLO_INTENCOES' },
  { label: 'Outro', value: 'OUTRO' },
]
