<script lang="ts">
	import { enhance } from '$app/forms';
	import Card from '$lib/components/core/Card.svelte';
	import TastePreferencesAdjustment from '$lib/components/TastePreferences.svelte';
	import { m } from '$lib/paraglide/messages.js';

	let { data } = $props();

	function handleDelete(event: MouseEvent) {
		const confirmed = confirm(m.delete_confirmation());
		if (!confirmed) {
			event.preventDefault();
		}
	}
</script>

{#snippet profileInfo(key: string, value: string | undefined)}
	<dt>{key}:</dt>
	<dd>{value ?? '—'}</dd>
{/snippet}
<div class="flex flex-1 justify-center">
	<div class="h-fit w-100">
		<Card>
			<div class="flex flex-col gap-4 p-4">
				<h2 class="border-b border-main-border">{m.profile()}</h2>
				<dl class="grid grid-cols-2 gap-3">
					{@render profileInfo(m.user_name(), data.user?.name)}
					{@render profileInfo(m.user_email(), data.user?.email)}
				</dl>

				<hr class="my-2 border-main-border" />
				<form class="flex flex-col gap-4" method="POST" use:enhance>
					<TastePreferencesAdjustment
						headerTitle={m.adjust_preferences()}
						initialPrepMethod={data.pref.prepMethod}
						initialExperience={data.pref.experienceLevel}
						formAction="?/preferences"
						initialData={data.pref.prepMethod + ', ' + data.pref.experienceLevel}
					/>

					<hr class="my-2 border-main-border" />

					<button
						class="btn-wide mt-4 bg-error font-semibold tracking-wide text-light hover:inset-shadow-md"
						type="submit"
						formaction="?/deleteAccount"
						onclick={handleDelete}>{m.delete_account()}</button
					>
				</form>
			</div>
		</Card>
	</div>
</div>
