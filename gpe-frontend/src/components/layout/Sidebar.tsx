import { NavLink } from 'react-router-dom'

interface NavItem {
  label: string
  icon: string
  to: string
  enabled: boolean
}

// Mesma ordem do protótipo. Habilite (enabled: true) conforme cada módulo for implementado.
const NAV_ITEMS: NavItem[] = [
  { label: 'Dash', icon: 'pi-chart-bar', to: '/dashboard', enabled: false },
  { label: 'Prosp', icon: 'pi-file', to: '/prospeccao', enabled: false },
  { label: 'Parceiros', icon: 'pi-users', to: '/parceiros', enabled: true },
  { label: 'Due Dil', icon: 'pi-shield', to: '/due-diligence', enabled: false },
  { label: 'Contratos', icon: 'pi-folder', to: '/contratos', enabled: false },
  { label: 'Exec', icon: 'pi-chart-line', to: '/execucao', enabled: false },
  { label: 'Financ', icon: 'pi-wallet', to: '/financeiro', enabled: false },
  { label: 'Gov', icon: 'pi-heart', to: '/governanca', enabled: false },
  { label: 'Result', icon: 'pi-print', to: '/resultados', enabled: false },
  { label: 'Encerra', icon: 'pi-sliders-h', to: '/encerramento', enabled: false },
  { label: 'Admin', icon: 'pi-cog', to: '/admin', enabled: false },
]

export function Sidebar() {
  return (
    <aside className="sticky top-0 flex h-screen w-[72px] shrink-0 flex-col items-center gap-1 border-r border-zinc-200 bg-white py-3">
      <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-full bg-zinc-700 text-xs font-bold text-white">
        S-P
      </div>

      {NAV_ITEMS.map((item) =>
        item.enabled ? (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex w-14 flex-col items-center gap-0.5 rounded-lg py-2 text-[10px] font-medium no-underline ${
                isActive ? 'bg-zinc-100 text-zinc-900' : 'text-zinc-500 hover:bg-zinc-50'
              }`
            }
          >
            <i className={`pi ${item.icon} text-lg`} />
            {item.label}
          </NavLink>
        ) : (
          <div
            key={item.to}
            title="Módulo ainda não implementado"
            className="flex w-14 cursor-not-allowed flex-col items-center gap-0.5 py-2 text-[10px] text-zinc-300"
          >
            <i className={`pi ${item.icon} text-lg`} />
            {item.label}
          </div>
        ),
      )}
    </aside>
  )
}
