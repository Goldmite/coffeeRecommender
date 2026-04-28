<script lang="ts">
	import { m } from '$lib/paraglide/messages';
	import type { ExperienceLevel, PrepMethod } from '$lib/types/recommendation';
	import { experienceMap, prepMethodMap } from '$lib/utils/mapping';
	import ActionButton from './core/ActionButton.svelte';
	import ProgressSlider from './core/ProgressSlider.svelte';

	const levels = Object.keys(experienceMap) as ExperienceLevel[];

	let selectedPrepMethod = $state('POUROVER');

	let selectedIdx = $state(0);
	const selectedLevel = $derived(levels[selectedIdx]);
	const experienceLevel = $derived(experienceMap[selectedLevel]);
</script>

<div class="onboarding-section space-y-3">
	<h3 class="font-semibold">{m.onboarding_title()}</h3>

	<div class="space-y-4">
		<label for="prepMethod">
			{m.prep_method()}:
		</label>

		<div class="mt-2 grid grid-cols-3 gap-2 text-sm">
			{#each Object.entries(prepMethodMap) as [key, { label }]}
				<button
					type="button"
					class="bean-border w-24 border-2 p-0.5
                {selectedPrepMethod === key
						? 'border-secondary bg-secondary/10'
						: 'border-main-border hover:bg-main-border/20'}"
					onclick={() => (selectedPrepMethod = key as PrepMethod)}
				>
					{label}
				</button>
			{/each}
		</div>
		<input type="hidden" name="prepMethod" value={selectedPrepMethod} />
	</div>

	<div>
		<div class="mb-1.5">
			<label for="experience">{m.xp_level()}:</label>
			<span class="ml-2 font-normal italic">{experienceLevel.label}</span>
		</div>
		<ProgressSlider bind:value={selectedIdx} min={0} max={4}></ProgressSlider>

		<input type="hidden" name="experience" value={selectedLevel} />
	</div>

	<ActionButton formAction="?/survey">{m.save()}</ActionButton>
</div>

<style>
	label {
		color: var(--color-secondary);
		font-weight: bolder;
	}
</style>
