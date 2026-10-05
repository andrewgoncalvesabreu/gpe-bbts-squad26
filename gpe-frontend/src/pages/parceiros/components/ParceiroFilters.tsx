import { Button } from 'primereact/button'
import { Dropdown } from 'primereact/dropdown'
import { InputText } from 'primereact/inputtext'
import { AREAS_ATUACAO, STATUS_OPTIONS, TIPO_ORGANIZACAO_OPTIONS } from '../../../constants/parceiro'

export interface ParceiroFiltersValue {
  busca: string
  tipo: string | null
  status: string | null
  area: string | null
}

export const emptyFilters: ParceiroFiltersValue = { busca: '', tipo: null, status: null, area: null }

interface Props {
  value: ParceiroFiltersValue
  onChange: (v: ParceiroFiltersValue) => void
}

export function ParceiroFilters({ value, onChange }: Props) {
  const set = <K extends keyof ParceiroFiltersValue>(key: K, v: ParceiroFiltersValue[K]) =>
    onChange({ ...value, [key]: v })

  return (
    <div className="grid grid-cols-1 items-end gap-4 rounded-xl border border-zinc-200 bg-white p-5 md:grid-cols-5">
      <div className="flex flex-col gap-1 md:col-span-2">
        <label className="text-sm font-medium">Buscar</label>
        <InputText
          value={value.busca}
          onChange={(e) => set('busca', e.target.value)}
          placeholder="Razão social, nome fantasia ou CNPJ..."
        />
      </div>
      <div className="flex flex-col gap-1">
        <label className="text-sm font-medium">Tipo de Instituição</label>
        <Dropdown
          value={value.tipo}
          options={TIPO_ORGANIZACAO_OPTIONS}
          onChange={(e) => set('tipo', e.value)}
          placeholder="Todos"
          showClear
        />
      </div>
      <div className="flex flex-col gap-1">
        <label className="text-sm font-medium">Status</label>
        <Dropdown
          value={value.status}
          options={STATUS_OPTIONS}
          onChange={(e) => set('status', e.value)}
          placeholder="Todos"
          showClear
        />
      </div>
      <div className="flex flex-col gap-1">
        <label className="text-sm font-medium">Área de Atuação</label>
        <div className="flex gap-2">
          <Dropdown
            className="flex-1"
            value={value.area}
            options={AREAS_ATUACAO}
            onChange={(e) => set('area', e.value)}
            placeholder="Todas"
            showClear
          />
          <Button type="button" label="Limpar" outlined onClick={() => onChange(emptyFilters)} />
        </div>
      </div>
    </div>
  )
}
