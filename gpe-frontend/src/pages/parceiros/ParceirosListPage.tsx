import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from 'primereact/button'
import { Column } from 'primereact/column'
import { DataTable } from 'primereact/datatable'
import { StatCard } from '../../components/common/StatCard'
import { useToast } from '../../components/common/toastContext'
import { TIPO_ORGANIZACAO_OPTIONS, labelFrom } from '../../constants/parceiro'
import { parceiroService } from '../../services/parceiroService'
import type { Parceiro } from '../../types/parceiro'
import { parseApiError } from '../../utils/apiError'
import { maskCnpj, onlyDigits } from '../../utils/format'
import { ParceiroFilters, emptyFilters, type ParceiroFiltersValue } from './components/ParceiroFilters'
import { ParceiroStatusTag } from './components/ParceiroStatusTag'

export default function ParceirosListPage() {
  const navigate = useNavigate()
  const toast = useToast()
  const [parceiros, setParceiros] = useState<Parceiro[]>([])
  const [loading, setLoading] = useState(true)
  const [filters, setFilters] = useState<ParceiroFiltersValue>(emptyFilters)

  useEffect(() => {
    parceiroService
      .listar()
      .then(setParceiros)
      .catch((err) => toast.error(parseApiError(err).message))
      .finally(() => setLoading(false))
  }, [toast])

  // O backend ainda não tem busca/paginação: filtramos no cliente.
  const filtrados = useMemo(() => {
    const termo = filters.busca.trim().toLowerCase()
    const termoDigitos = onlyDigits(termo)
    return parceiros.filter((p) => {
      if (termo) {
        const texto = `${p.razaoSocial} ${p.nomeFantasia ?? ''}`.toLowerCase()
        const bateTexto = texto.includes(termo)
        const bateCnpj = termoDigitos.length > 0 && p.cnpj.includes(termoDigitos)
        if (!bateTexto && !bateCnpj) return false
      }
      if (filters.tipo && p.tipoOrganizacao !== filters.tipo) return false
      if (filters.status && p.status !== filters.status) return false
      if (filters.area && !(p.segmentoAtuacao ?? '').split(',').map((a) => a.trim()).includes(filters.area)) return false
      return true
    })
  }, [parceiros, filters])

  const total = parceiros.length
  const ativos = parceiros.filter((p) => p.status === 'ATIVO').length
  const emAvaliacao = parceiros.filter((p) => p.status === 'EM_ANALISE').length
  const inativos = parceiros.filter((p) => p.status === 'INATIVO' || p.status === 'BLOQUEADO').length

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Parceiros Cadastrados</h1>
        <Button type="button" icon="pi pi-plus" label="Novo Parceiro" onClick={() => navigate('/parceiros/novo')} />
      </div>

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Total de Parceiros" value={total} hint="Cadastrados no sistema" />
        <StatCard label="Ativos" value={ativos} hint="Com parcerias vigentes" />
        <StatCard label="Em Avaliação" value={emAvaliacao} hint="Aguardando Due Diligence" />
        <StatCard label="Inativos/Bloqueados" value={inativos} hint="Necessitam regularização" />
      </div>

      <ParceiroFilters value={filters} onChange={setFilters} />

      <div className="rounded-xl border border-zinc-200 bg-white p-2">
        <DataTable
          value={filtrados}
          loading={loading}
          dataKey="id"
          paginator
          rows={10}
          rowsPerPageOptions={[10, 25, 50]}
          rowClassName={() => 'cursor-pointer'}
          onRowClick={(e) => navigate(`/parceiros/${(e.data as Parceiro).id}`)}
          emptyMessage="Nenhum parceiro encontrado."
          currentPageReportTemplate="Exibindo {first} a {last} de {totalRecords} parceiros"
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
        >
          <Column
            header="Nome"
            body={(p: Parceiro) => (
              <div>
                <div className="font-semibold text-zinc-900">{p.razaoSocial}</div>
                {p.nomeFantasia && <div className="text-xs text-zinc-500">{p.nomeFantasia}</div>}
              </div>
            )}
          />
          <Column header="CNPJ" body={(p: Parceiro) => maskCnpj(p.cnpj)} />
          <Column header="Tipo" body={(p: Parceiro) => labelFrom(TIPO_ORGANIZACAO_OPTIONS, p.tipoOrganizacao)} />
          <Column
            header="Área Principal"
            body={(p: Parceiro) => p.segmentoAtuacao?.split(',')[0]?.trim() || '—'}
          />
          <Column header="Status" body={(p: Parceiro) => <ParceiroStatusTag status={p.status} />} />
          <Column
            header="Ações"
            style={{ width: '7rem' }}
            body={(p: Parceiro) => (
              <div className="flex gap-1" onClick={(e) => e.stopPropagation()}>
                <Button type="button" icon="pi pi-eye" text rounded aria-label="Ver" onClick={() => navigate(`/parceiros/${p.id}`)} />
                <Button type="button" icon="pi pi-pencil" text rounded aria-label="Editar" onClick={() => navigate(`/parceiros/${p.id}/editar`)} />
              </div>
            )}
          />
        </DataTable>
      </div>
    </div>
  )
}
