<script lang="ts">
	import { enhance } from '$app/forms';
	import { m } from '$lib/paraglide/messages';
	import SubmitButton from './core/SubmitButton.svelte';

	let formElement: HTMLFormElement;
	let {
		formId,
		id,
		isPurchased,
		onSuccess,
	}: { formId: string; id: number; isPurchased?: boolean; onSuccess?: () => void } = $props();

	export function requestSubmit() {
		formElement.requestSubmit();
	}
</script>

<form
	bind:this={formElement}
	id={formId}
	class="p-4"
	method="POST"
	action="?/purchased"
	use:enhance={() => {
		return async ({ result }) => {
			if (result.type === 'success') {
				onSuccess?.();
			}
		};
	}}
>
	<input name="coffeeId" value={id} type="hidden" />
	<input name="purchased" value={isPurchased} type="hidden" />
	{#if isPurchased}
		<SubmitButton>{m.yes()}</SubmitButton>
	{/if}
</form>
