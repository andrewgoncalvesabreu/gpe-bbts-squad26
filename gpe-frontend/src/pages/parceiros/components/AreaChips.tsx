import { AREAS_ATUACAO } from '../../../constants/parceiro'

interface AreaChipsProps {
  value: string[]
  onChange: (areas: string[]) => void
}

export function AreaChips({ value, onChange }: AreaChipsProps) {
  const toggle = (area: string) =>
    onChange(value.includes(area) ? value.filter((a) => a !== area) : [...value, area])

  return (
    <div className="flex flex-wrap gap-2">
      {AREAS_ATUACAO.map((area) => {
        const selected = value.includes(area)
        return (
          <button
            key={area}
            type="button"
            aria-pressed={selected}
            onClick={() => toggle(area)}
            className={`rounded-full border px-4 py-1.5 text-sm transition ${
              selected
                ? 'border-zinc-700 bg-zinc-700 text-white'
                : 'border-zinc-300 bg-white text-zinc-700 hover:bg-zinc-100'
            }`}
          >
            {area}
          </button>
        )
      })}
    </div>
  )
}
