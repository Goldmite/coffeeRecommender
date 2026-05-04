<script lang="ts">
	import { m } from '$lib/paraglide/messages';
	import type { ExperienceLevel, PrepMethod } from '$lib/types/recommendation';
	import { experienceMap, prepMethodMap } from '$lib/utils/mapping';
	import ActionButton from './core/ActionButton.svelte';
	import ProgressSlider from './core/ProgressSlider.svelte';

	let {
		headerTitle = m.onboarding_title(),
		initialPrepMethod = 'POUROVER',
		initialExperience = 'BEGINNER',
		formAction = '?/survey',
		initialData = '',
	} = $props();

	const levels = Object.keys(experienceMap) as ExperienceLevel[];

	// svelte-ignore state_referenced_locally
	let selectedPrepMethod = $state(initialPrepMethod);
	// svelte-ignore state_referenced_locally
	let selectedIdx = $state(levels.indexOf(initialExperience as ExperienceLevel) ?? 0);
	const selectedLevel = $derived(levels[selectedIdx]);
	const experienceLevel = $derived(experienceMap[selectedLevel]);

	let isDisabled = $derived(
		initialData.includes(selectedPrepMethod) && initialData.includes(selectedLevel),
	);
</script>

<div class="onboarding-section space-y-3">
	<h3 class="font-semibold">{headerTitle}</h3>

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
		<ProgressSlider bind:value={selectedIdx} min={0} max={3}></ProgressSlider>

		<input type="hidden" name="experience" value={selectedLevel} />
	</div>

	<ActionButton {formAction} disabled={isDisabled}>{m.save()}</ActionButton>
</div>

<style>
	label {
		color: var(--color-secondary);
		font-weight: bolder;
	}
</style>
