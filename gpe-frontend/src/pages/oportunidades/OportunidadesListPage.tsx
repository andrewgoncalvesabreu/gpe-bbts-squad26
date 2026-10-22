import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from 'primereact/button'
import { Column } from 'primereact/column'
import { DataTable } from 'primereact/datatable'
import { Dropdown } from 'primereact/dropdown'
import { InputText } from 'primereact/inputtext'
import { StatCard } from '../../components/common/StatCard'
import { useToast } from '../../components/common/toastContext'
import { STATUS_OPORTUNIDADE_OPTIONS, labelOrigemOportunidade } from '../../constants/oportunidade'
import { oportunidadeService } from '../../services/oportunidadeService'
import type { Oportunidade } from '../../types/oportunidade'
import { parseApiError } from '../../utils/apiError'
import { formatDate, formatMoney } from '../../utils/format'
import { OportunidadeStatusTag } from './components/OportunidadeStatusTag'

export default function OportunidadesListPage() {
  const navigate = useNavigate()
  const toast = useToast()
  const [oportunidades, setOportunidades] = useState<Oportunidade[]>([])
  const [loading, setLoading] = useState(true)
  const [busca, setBusca] = useState('')
  const [status, setStatus] = useState<string | null>(null)

  useEffect(() => {
    oportunidadeService.listar()
      .then(setOportunidades)
      .catch((err) => toast.error(parseApiError(err).message))
      .finally(() => setLoading(false))
  }, [toast])

  const filtradas = useMemo(() => {
    const termo = busca.trim().toLowerCase()
    return oportunidades.filter((oportunidade) => {
      const texto = `${oportunidade.titulo} ${oportunidade.parceiro?.razaoSocial ?? ''} ${oportunidade.areaDemandante}`.toLowerCase()
      return (!termo || texto.includes(termo)) && (!status || oportunidade.statusWorkflow === status)
    })
  }, [busca, oportunidades, status])

  const total = oportunidades.length
  const emAvaliacao = oportunidades.filter((item) => item.statusWorkflow === 'EM_AVALIACAO').length
  const aprovadas = oportunidades.filter((item) => item.statusWorkflow === 'APROVADA').length
  const valorEstimado = oportunidades.reduce((totalAtual, item) => totalAtual + Number(item.estimativaFinanceira ?? 0), 0)

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Oportunidades de Parceria</h1>
          <p className="mt-1 text-sm text-zinc-500">Acompanhe a prospecção, avaliação e aprovação de novas oportunidades.</p>
        </div>
        <Button type="button" icon="pi pi-plus" label="Nova Oportunidade" onClick={() => navigate('/oportunidades/novo')} />
      </div>

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Total" value={total} hint="Oportunidades registradas" />
        <StatCard label="Em avaliação" value={emAvaliacao} hint="Aguardando análise" />
        <StatCard label="Aprovadas" value={aprovadas} hint="Prontas para formalização" />
        <StatCard label="Valor estimado" value={formatMoney(valorEstimado)} hint="Soma das estimativas" />
      </div>

      <div className="grid grid-cols-1 items-end gap-4 rounded-xl border border-zinc-200 bg-white p-5 md:grid-cols-3">
        <div className="flex flex-col gap-1 md:col-span-2">
          <label htmlFor="busca-oportunidade" className="text-sm font-medium">Buscar</label>
          <InputText id="busca-oportunidade" value={busca} onChange={(event) => setBusca(event.target.value)} placeholder="Título, parceiro ou área demandante..." />
        </div>
        <div className="flex flex-col gap-1">
          <label htmlFor="status-oportunidade" className="text-sm font-medium">Status</label>
          <Dropdown id="status-oportunidade" value={status} options={STATUS_OPORTUNIDADE_OPTIONS} onChange={(event) => setStatus(event.value)} placeholder="Todos" showClear />
        </div>
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white p-2">
        <DataTable
          value={filtradas}
          loading={loading}
          dataKey="id"
          paginator
          rows={10}
          rowsPerPageOptions={[10, 25, 50]}
          rowClassName={() => 'cursor-pointer'}
          onRowClick={(event) => navigate(`/oportunidades/${(event.data as Oportunidade).id}`)}
          emptyMessage="Nenhuma oportunidade encontrada."
          currentPageReportTemplate="Exibindo {first} a {last} de {totalRecords} oportunidades"
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
        >
          <Column header="Oportunidade" body={(item: Oportunidade) => <div><div className="font-semibold text-zinc-900">{item.titulo}</div><div className="text-xs text-zinc-500">{item.parceiro?.razaoSocial ?? 'Parceiro não informado'}</div></div>} />
          <Column header="Origem" body={(item: Oportunidade) => labelOrigemOportunidade(item.origem)} />
          <Column header="Área demandante" field="areaDemandante" />
          <Column header="Criada em" body={(item: Oportunidade) => formatDate(item.dataCriacao)} />
          <Column header="Estimativa" body={(item: Oportunidade) => formatMoney(item.estimativaFinanceira)} />
          <Column header="Status" body={(item: Oportunidade) => <OportunidadeStatusTag status={item.statusWorkflow} />} />
          <Column header="Ações" style={{ width: '7rem' }} body={(item: Oportunidade) => <div onClick={(event) => event.stopPropagation()}><Button type="button" icon="pi pi-eye" text rounded aria-label="Ver oportunidade" onClick={() => navigate(`/oportunidades/${item.id}`)} /></div>} />
        </DataTable>
      </div>
    </div>
  )
}
