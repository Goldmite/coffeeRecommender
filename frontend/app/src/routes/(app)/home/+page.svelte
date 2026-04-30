<script lang="ts">
	import { enhance } from '$app/forms';
	import Card from '$lib/components/core/Card.svelte';
	import Modal from '$lib/components/core/Modal.svelte';
	import GenerateButton from '$lib/components/core/SubmitButton.svelte';
	import DetailsModalContent from '$lib/components/DetailsModalContent.svelte';
	import FeatureFilter from '$lib/components/FeatureFilter.svelte';
	import PurchaseQueryForm from '$lib/components/PurchaseQueryForm.svelte';
	import RecommendationList from '$lib/components/RecommendationList.svelte';
	import ShopSelection from '$lib/components/ShopSelection.svelte';
	import TasteOnboarding from '$lib/components/TasteOnboarding.svelte';
	import { m } from '$lib/paraglide/messages.js';
	import type { CoffeeBeanResponse, ShopResponse } from '$lib/types/recommendation';
	import { tick } from 'svelte';
	import type { PageProps } from './$types';

	let { data, form }: PageProps = $props();

	let loading = $state(false);
	const count = $derived(form?.recommendations?.length ?? 0);
	const isOnboarded = $derived(form?.success || !data.needsOnboarding);

	let selectedCoffee = $state<CoffeeBeanResponse | undefined>(undefined);
	let detailsModalRef: ReturnType<typeof Modal>;
	function handleShowDetails(coffee: CoffeeBeanResponse) {
		selectedCoffee = coffee;
		detailsModalRef.open();
	}
	let clickedCoffeeId = $state<number | undefined>(undefined);
	let purchaseQueryModalRef: ReturnType<typeof Modal>;
	let clickedFormElementRef: ReturnType<typeof PurchaseQueryForm> | undefined = $state(undefined);
	async function handlePurchaseQuery(coffeeId: number) {
		clickedCoffeeId = coffeeId;

		await tick(); // render if block to bind ref

		if (clickedFormElementRef) {
			clickedFormElementRef.requestSubmit();
		}
		purchaseQueryModalRef.open();
	}

	const shops: ShopResponse[] = $derived(data.shopList);
	const lastUsedShops = $derived(form?.shopIds ? form.shopIds.map((id) => Number(id)) : []);
</script>

<div class="flex flex-row gap-7">
	<div class="max-h-fit min-w-60">
		<Card>
			<form
				id="rec-form"
				class="p-4 {isOnboarded ? 'bg-primary/10' : 'bg-main'}"
				method="POST"
				action="?/fetch"
				use:enhance={() => {
					loading = true;
					return async ({ update }) => {
						await update({ reset: false });
						loading = false;
					};
				}}
			>
				{#if isOnboarded}
					<ShopSelection {shops} lastSelectedIds={lastUsedShops} />
					<GenerateButton disabled={loading}>
						{#if loading}
							<span class="icon-[svg-spinners--180-ring]"></span>
						{:else}
							{m.get_recommendations()}
						{/if}</GenerateButton
					>
				{:else}
					<TasteOnboarding></TasteOnboarding>
				{/if}
			</form>
		</Card>
	</div>
	<div class="w-200 space-y-2">
		{#if isOnboarded}<FeatureFilter />{/if}
		<Card>
			<RecommendationList
				recommendations={form?.recommendations}
				onShowDetails={handleShowDetails}
				onUrlClick={handlePurchaseQuery}
			/>
		</Card>
		<span class="ml-2 text-sm text-main-border italic">{m.recommendation_count({ count })}.</span>
	</div>
</div>

<Modal bind:this={detailsModalRef} headerTxt={selectedCoffee?.name}>
	{#if selectedCoffee}
		<DetailsModalContent details={selectedCoffee} />
	{/if}
</Modal>

<Modal bind:this={purchaseQueryModalRef} headerTxt={m.purchase_query()}>
	{#if clickedCoffeeId}
		<PurchaseQueryForm
			formId="buy-form"
			id={clickedCoffeeId}
			isPurchased={true}
			onSuccess={() => purchaseQueryModalRef.close()}
		/>
	{/if}
</Modal>
{#if clickedCoffeeId}
	<PurchaseQueryForm bind:this={clickedFormElementRef} formId="pre-query" id={clickedCoffeeId} />
{/if}
