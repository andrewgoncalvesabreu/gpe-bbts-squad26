import { Tag } from 'primereact/tag'
import { STATUS_OPORTUNIDADE_SEVERITY, labelStatusOportunidade } from '../../../constants/oportunidade'
import type { StatusOportunidade } from '../../../types/oportunidade'

export function OportunidadeStatusTag({ status }: { status: StatusOportunidade }) {
  return <Tag value={labelStatusOportunidade(status)} severity={STATUS_OPORTUNIDADE_SEVERITY[status] ?? 'secondary'} />
}
