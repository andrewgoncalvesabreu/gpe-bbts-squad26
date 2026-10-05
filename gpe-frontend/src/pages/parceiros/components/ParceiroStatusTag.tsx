import { Tag } from 'primereact/tag'
import { STATUS_OPTIONS, STATUS_SEVERITY, labelFrom } from '../../../constants/parceiro'

export function ParceiroStatusTag({ status }: { status: string }) {
  return <Tag value={labelFrom(STATUS_OPTIONS, status)} severity={STATUS_SEVERITY[status] ?? 'secondary'} />
}
