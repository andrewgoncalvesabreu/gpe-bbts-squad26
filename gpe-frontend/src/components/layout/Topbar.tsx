import { useLocation } from 'react-router-dom'

function buildBreadcrumb(pathname: string): string {
  const parts = ['SIGES-PES']
  const segs = pathname.split('/').filter(Boolean)
  if (segs[0] === 'parceiros') {
    parts.push('Parceiros')
    if (segs[1] === 'novo') parts.push('Novo Parceiro')
    else if (segs[1] && segs[2] === 'editar') parts.push('Editar Parceiro')
    else if (segs[1]) parts.push('Detalhes')
  }
  return parts.join(' > ')
}

export function Topbar() {
  const { pathname } = useLocation()

  return (
    <header className="flex h-14 items-center justify-between border-b border-zinc-200 bg-white px-6">
      <span className="text-sm font-medium text-zinc-800">{buildBreadcrumb(pathname)}</span>
      <div className="flex items-center gap-3 text-sm text-zinc-700">
        <i className="pi pi-bell text-zinc-500" />
        <span className="h-5 w-px bg-zinc-200" />
        <span className="flex h-8 w-8 items-center justify-center rounded-full bg-zinc-700 text-xs font-semibold text-white">
          US
        </span>
        <span>Gestor SIGES</span>
      </div>
    </header>
  )
}
