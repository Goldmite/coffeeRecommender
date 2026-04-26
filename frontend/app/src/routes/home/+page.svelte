<script lang="ts">
	import { enhance } from '$app/forms';
	import Card from '$lib/components/core/Card.svelte';
	import GenerateButton from '$lib/components/core/SubmitButton.svelte';
	import RecommendationList from '$lib/components/RecommendationList.svelte';
	import { m } from '$lib/paraglide/messages.js';

	let { form } = $props();

	let loading = $state(false);
</script>

<form
	method="POST"
	action="?/fetch"
	use:enhance={() => {
		loading = true;
		return async ({ update }) => {
			await update();
			loading = false;
		};
	}}
>
	<GenerateButton disabled={loading}>
		{#if loading}
			<span class="icon-[svg-spinners--180-ring]"></span>
		{:else}
			{m.get_recommendations()}
		{/if}</GenerateButton
	>
</form>
<Card>
	<RecommendationList recommendations={form?.recommendations}></RecommendationList>
</Card>
